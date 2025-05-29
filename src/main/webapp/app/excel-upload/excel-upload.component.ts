import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface UploadParams {
  batchNo: string;
  branchCode: string;
  sourceCode: string;
  exchRate: number;
  entryDate: string;
}

interface UploadResult {
  success: boolean;
  message: string;
  batchNo: string;
  totalRows: number;
  successCount: number;
  errorCount: number;
  errors: UploadError[];
}

interface UploadError {
  rowNumber: number;
  errorMessage: string;
  relCust?: string;
  account?: string;
  accountBranch?: string;
  drCr?: string;
  ccyCd?: string;
  amount?: number;
  lcyEquivalent?: number;
  txnCode?: string;
  addlText?: string;
}

interface Branch {
  branch_code: string;
  branch_name: string;
}

interface SourceCode {
  source_code: string;
}

@Component({
  selector: 'app-excel-upload',
  templateUrl: './excel-upload.component.html',
  imports: [ReactiveFormsModule],
  styleUrls: ['./excel-upload.component.scss'],
})
export class ExcelUploadComponent implements OnInit {
  uploadForm: FormGroup;
  selectedFile: File | null = null;
  uploading = false;
  uploadResult: UploadResult | null = null;
  branches: Branch[] = [];
  sourceCodes: SourceCode[] = [];
  batches: any[] = [];

  private apiUrl = '/api/excel-upload';

  constructor(
    private http: HttpClient,
    private formBuilder: FormBuilder,
  ) {
    this.uploadForm = this.formBuilder.group({
      batchNo: ['', [Validators.required, Validators.maxLength(20)]],
      branchCode: ['', [Validators.required]],
      sourceCode: ['', [Validators.required]],
      exchRate: [1, [Validators.required, Validators.min(0.000001)]],
      entryDate: [this.getCurrentDate(), [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.loadBranches();
    this.loadSourceCodes();
    this.loadBatchSummary();
  }

  /**
   * Load branches for dropdown
   */
  loadBranches(): void {
    this.http.get<Branch[]>(`${this.apiUrl}/branches`).subscribe({
      next: branches => {
        this.branches = branches;
        if (branches.length > 0) {
          this.uploadForm.patchValue({ branchCode: branches[0].branch_code });
          this.onBranchChange(branches[0].branch_code);
        }
      },
      error: error => {
        console.error('Error loading branches:', error);
        this.showError('Failed to load branches');
      },
    });
  }

  /**
   * Load source codes for dropdown
   */
  loadSourceCodes(): void {
    this.http.get<SourceCode[]>(`${this.apiUrl}/source-codes`).subscribe({
      next: sourceCodes => {
        this.sourceCodes = sourceCodes;
        if (sourceCodes.length > 0) {
          this.uploadForm.patchValue({ sourceCode: sourceCodes[0].source_code });
        }
      },
      error: error => {
        console.error('Error loading source codes:', error);
        this.showError('Failed to load source codes');
      },
    });
  }

  /**
   * Load batch summary
   */
  loadBatchSummary(): void {
    this.http.get<any[]>(`${this.apiUrl}/batches`).subscribe({
      next: batches => {
        this.batches = batches.map(batch => ({
          batchNo: batch[0],
          recordCount: batch[1],
        }));
      },
      error: error => {
        console.error('Error loading batch summary:', error);
      },
    });
  }

  /**
   * Handle branch change to load working day
   */
  onBranchChange(branchCode: string): void {
    if (branchCode) {
      this.http.get<any>(`${this.apiUrl}/working-day/${branchCode}`).subscribe({
        next: response => {
          this.uploadForm.patchValue({ entryDate: response.workingDay });
        },
        error: error => {
          console.error('Error loading working day:', error);
        },
      });
    }
  }

  /**
   * Handle file selection
   */
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel'];

      if (validTypes.includes(file.type)) {
        this.selectedFile = file;
        this.uploadResult = null;
      } else {
        this.showError('Please select a valid Excel file (.xlsx or .xls)');
        this.selectedFile = null;
        event.target.value = '';
      }
    }
  }

  /**
   * Upload Excel file
   */
  uploadFile(): void {
    if (!this.selectedFile || this.uploadForm.invalid) {
      this.showError('Please select a file and fill all required fields');
      return;
    }

    // Check if batch exists
    const batchNo = this.uploadForm.get('batchNo')?.value;
    this.checkBatchExists(batchNo).subscribe({
      next: exists => {
        if (exists) {
          this.showError(`Batch ${batchNo} already exists in the system`);
          return;
        }
        this.performUpload();
      },
      error: error => {
        console.error('Error checking batch:', error);
        this.performUpload(); // Continue with upload if check fails
      },
    });
  }

  /**
   * Perform the actual file upload
   */
  private performUpload(): void {
    if (!this.selectedFile) return;

    this.uploading = true;
    this.uploadResult = null;

    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('batchNo', this.uploadForm.get('batchNo')?.value);
    formData.append('branchCode', this.uploadForm.get('branchCode')?.value);
    formData.append('sourceCode', this.uploadForm.get('sourceCode')?.value);
    formData.append('exchRate', this.uploadForm.get('exchRate')?.value.toString());
    formData.append('entryDate', this.uploadForm.get('entryDate')?.value);

    this.http.post<UploadResult>(`${this.apiUrl}/upload`, formData).subscribe({
      next: result => {
        this.uploadResult = result;
        this.uploading = false;
        if (result.success) {
          this.showSuccess(result.message);
          this.loadBatchSummary(); // Refresh batch list
        } else {
          this.showWarning(result.message);
        }
      },
      error: error => {
        console.error('Upload error:', error);
        this.uploading = false;
        if (error.error && error.error.message) {
          this.uploadResult = error.error;
          this.showError(error.error.message);
        } else {
          this.showError('Upload failed. Please try again.');
        }
      },
    });
  }

  /**
   * Delete batch
   */
  deleteBatch(): void {
    const batchNo = this.uploadForm.get('batchNo')?.value;
    if (!batchNo) {
      this.showError('Please enter a batch number');
      return;
    }

    if (confirm(`Are you sure you want to delete batch ${batchNo}?`)) {
      this.http.delete<any>(`${this.apiUrl}/batch/${batchNo}`).subscribe({
        next: response => {
          if (response.success) {
            this.showSuccess(response.message);
            this.loadBatchSummary();
            this.uploadResult = null;
          } else {
            this.showError(response.message);
          }
        },
        error: error => {
          console.error('Delete error:', error);
          if (error.error && error.error.message) {
            this.showError(error.error.message);
          } else {
            this.showError('Failed to delete batch');
          }
        },
      });
    }
  }

  /**
   * Download Excel template
   */
  downloadTemplate(): void {
    this.http.get(`${this.apiUrl}/template`, { responseType: 'blob' }).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'upload_template.xlsx';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: error => {
        console.error('Download error:', error);
        this.showError('Failed to download template');
      },
    });
  }

  /**
   * Export error report
   */
  exportErrorReport(): void {
    if (!this.uploadResult || !this.uploadResult.errors.length) {
      this.showError('No errors to export');
      return;
    }

    // Create CSV content
    const headers = [
      'Row',
      'Error',
      'Customer',
      'Account',
      'Branch',
      'Dr/Cr',
      'Currency',
      'Amount',
      'LCY Equivalent',
      'Txn Code',
      'Additional Text',
    ];
    const csvContent = [
      headers.join(','),
      ...this.uploadResult.errors.map(error =>
        [
          error.rowNumber,
          `"${error.errorMessage}"`,
          error.relCust || '',
          error.account || '',
          error.accountBranch || '',
          error.drCr || '',
          error.ccyCd || '',
          error.amount || '',
          error.lcyEquivalent || '',
          error.txnCode || '',
          `"${error.addlText || ''}"`,
        ].join(','),
      ),
    ].join('\n');

    // Download CSV
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `batch_${this.uploadResult.batchNo}_errors.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  /**
   * Clear form and reset
   */
  clearForm(): void {
    this.uploadForm.reset({
      exchRate: 1,
      entryDate: this.getCurrentDate(),
    });
    this.selectedFile = null;
    this.uploadResult = null;

    // Reset file input
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  /**
   * Check if batch exists
   */
  private checkBatchExists(batchNo: string): Observable<boolean> {
    if (!batchNo) return of(false);

    return this.http.get<any>(`${this.apiUrl}/batch/${batchNo}/exists`).pipe(
      map(response => response.exists),
      catchError(() => of(false)),
    );
  }

  /**
   * Get current date in YYYY-MM-DD format
   */
  private getCurrentDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  /**
   * Show success message
   */
  private showSuccess(message: string): void {
    // Implement your notification service here
    alert('Success: ' + message);
  }

  /**
   * Show error message
   */
  private showError(message: string): void {
    // Implement your notification service here
    alert('Error: ' + message);
  }

  /**
   * Show warning message
   */
  private showWarning(message: string): void {
    // Implement your notification service here
    alert('Warning: ' + message);
  }

  /**
   * Get form control for validation
   */
  getFormControl(name: string) {
    return this.uploadForm.get(name);
  }

  /**
   * Check if form control has error
   */
  hasError(name: string, errorType: string): boolean {
    const control = this.getFormControl(name);
    return !!(control && control.hasError(errorType) && (control.dirty || control.touched));
  }
}

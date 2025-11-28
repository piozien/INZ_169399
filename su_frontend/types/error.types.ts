export interface ProblemDetail {
    title?: string;
    status?: number;
    detail?: string;
    fieldErrors?: Array<{ field: string; message: string }>;
}

export class ApiError extends Error {
    public status: number;
    public details: ProblemDetail;

    constructor(message: string, status: number, details: ProblemDetail) {
        super(message);
        this.status = status;
        this.details = details;
        Object.setPrototypeOf(this, ApiError.prototype);
    }
}
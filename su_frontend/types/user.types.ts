export interface User {
  id: string;
  fullName: string;
  email: string;
  status: "PENDING" | "CONFIRMED" | "BLOCKED";
  classId?: string;
  createdAt: string;
  authProvider: "LOCAL" | "MICROSOFT";
  externalId?: string;
  roles?: string[];
}

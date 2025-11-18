export interface StudentClass {
  id: string;
  name: string;
}

export interface Council {
  id: string;
  name: string;
}

export interface User {
  id: string;
  fullName: string;
  email: string;
  status: "PENDING" | "CONFIRMED" | "BLOCKED";
  createdAt: string;
  authProvider: "LOCAL" | "MICROSOFT";
  externalId?: string;
  roles?: string[];
  studentClass?: StudentClass;
  council?: Council;
}

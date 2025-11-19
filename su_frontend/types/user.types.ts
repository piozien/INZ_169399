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
  status: string;
  createdAt: string;
  authProvider: string;
  externalId?: string;
  studentClass?: {
    id: string;
    name: string;
  };
  council?: {
    id:string;
    name: string;
  };
  roles: string[];
  permissions: string[];
}

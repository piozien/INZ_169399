export type RoleCode =
  | 'DYREKTOR'
  | 'ZASTEPCA_DYREKTORA'
  | 'OPIEKUN_SU'
  | 'NAUCZYCIEL'
  | 'PRZEWODNICZACY_SU'
  | 'ZASTEPCA_SU'
  | 'SKARBNIK_SU'
  | 'CZLONEK_SU'
  | 'BYLY_CZLONEK_SU'
  | 'UCZEN'
  | 'BYLY_UCZEN'
  | 'ADMINISTRATOR';

export type RoleCategory =
  | 'SCHOOL_MANAGEMENT'
  | 'TEACHERS'
  | 'SU'
  | 'OTHER'
  | 'SYSTEM';

export type StatusEnum = 'CONFIRMED' | 'PENDING' | 'BLOCKED';

export type AuthProvider = 'MICROSOFT' | 'LOCAL';

export type EventStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export type EventParticipantRole = 'ORGANIZER' | 'PARTICIPANT';

export interface EnumsResponse {
  statuses: StatusEnum[];
  authProviders: AuthProvider[];
  roleCategories: RoleCategory[];
  roleCodes: RoleCode[];
  transactionTypes: string[];
  eventParticipantRoles: EventParticipantRole[];
  suggestionStatuses: string[];
  actionTypes: string[];
  eventStatuses: EventStatus[];
}


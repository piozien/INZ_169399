const API_URL = process.env.NEXT_PUBLIC_API_URL;

export interface PermissionsResponse {
  roles: string[];
  permissions: string[];
}


export async function fetchUserPermissions(): Promise<PermissionsResponse> {
  const response = await fetch(`${API_URL}/permissions`, {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch permissions');
  }

  return response.json();
}

export async function hasPermission(permission: string): Promise<boolean> {
  try {
    const { permissions } = await fetchUserPermissions();
    return permissions.includes(permission);
  } catch {
    return false;
  }
}


import { ApiError } from "@/types/error.types";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

let isRefreshing = false;

export async function apiFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    
    const headers = new Headers(options.headers);
    
    if (options.body && !headers.has('Content-Type') && !(options.body instanceof FormData)) {
        headers.set('Content-Type', 'application/json');
    }

    const defaultOptions: RequestInit = {
        ...options,
        headers,
        credentials: 'include', 
    };

    let response = await fetch(`${API_URL}${endpoint}`, defaultOptions);

    if (response.status === 401 && !isRefreshing) {
        if (!endpoint.includes('/auth/login')) {
            isRefreshing = true;

            try {
                const refreshRes = await fetch(`${API_URL}/auth/refresh`, {
                    method: 'POST',
                    credentials: 'include'
                });

                if (refreshRes.ok) {
                    isRefreshing = false;
                    response = await fetch(`${API_URL}${endpoint}`, defaultOptions);
                } else {
                    isRefreshing = false;
                    
                    if (typeof window !== 'undefined' && !endpoint.includes('/users/me')) {
                        window.location.href = '/login';
                    }
                    
                    throw new Error("Session expired");
                }
            } catch (error) {
                isRefreshing = false;
                if (error instanceof Error && error.message === "Session expired") {
                    throw error;
                }
            }
        }
    }

    if (!response.ok) {
        let errorData;
        try { 
            errorData = await response.json(); 
        } catch { 
            errorData = {}; 
        }
        
        //  ProblemDetail (Spring Boot 3)
        const message = errorData.detail || errorData.message || errorData.title || 'Wystąpił błąd serwera';
        
        throw new ApiError(message, response.status, errorData);
    }

    // (204 No Content)
    if (response.status === 204) {
        return {} as T;
    }

    return response.json() as Promise<T>;
}
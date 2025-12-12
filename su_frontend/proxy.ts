import {NextResponse} from 'next/server';
import type {NextRequest} from 'next/server';

const protectedRoutes = ['/dashboard'];

const authRoutes = ['/login', '/register'];

export function proxy(request: NextRequest) {
    const {pathname} = request.nextUrl;

    const hasAuthToken = request.cookies.has('refreshToken') || request.cookies.has('accessToken');

    const isProtectedRoute = protectedRoutes.some((route) => pathname.startsWith(route));

    if (isProtectedRoute) {
        if (!hasAuthToken) {
            const url = new URL('/login', request.url);
            url.searchParams.set('returnUrl', pathname);
            return NextResponse.redirect(url);
        }
    }

    const isAuthRoute = authRoutes.includes(pathname);

    if (isAuthRoute) {
        if (hasAuthToken) {
            return NextResponse.redirect(new URL('/dashboard', request.url));
        }
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        '/((?!api|_next/static|_next/image|favicon.ico|.*\\..*).*)',
    ],
};
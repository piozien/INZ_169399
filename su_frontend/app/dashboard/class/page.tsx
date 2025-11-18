'use client';

import { useCurrentUser } from '@/lib/hooks/useCurrentUser';

export default function ClassPage() {
  const { data: user, isLoading } = useCurrentUser();

  if (isLoading) {
    return <div>Ładowanie...</div>;
  }

  if (user?.studentClass) {
    return (
      <div>
        <h1>Strona Twojej Klasy: {user.studentClass.name}</h1>
        <p>Witaj w panelu swojej klasy. Tutaj znajdziesz informacje o członkach i finansach klasowych.</p>
        <p>ID Klasy: {user.studentClass.id}</p>
      </div>
    );
  }

  return (
    <div>
      <h1>Panel Klasy</h1>
      <p>Nie jesteś przypisany(a) do żadnej konkretnej klasy.</p>
      <p>Administratorzy i nauczyciele mogą mieć dostęp do widoku wszystkich klas (do zaimplementowania).</p>
    </div>
  );
}

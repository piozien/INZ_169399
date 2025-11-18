'use client';

import { useCurrentUser } from '@/lib/hooks/useCurrentUser';

export default function CouncilPage() {
  const { data: user, isLoading } = useCurrentUser();

  if (isLoading) {
    return <div>Ładowanie...</div>;
  }

  if (user?.council) {
    return (
      <div>
        <h1>Strona Samorządu: {user.council.name}</h1>
        <p>Witaj w panelu swojego samorządu. Tutaj znajdziesz informacje o członkach, finansach i wydarzeniach.</p>
        <p>ID Samorządu: {user.council.id}</p>
      </div>
    );
  }

  return (
    <div>
      <h1>Dołącz do Samorządu</h1>
      <p>Wygląda na to, że nie należysz jeszcze do żadnego samorządu.</p>
      <form>
        <label htmlFor="join-code">Wprowadź kod dołączenia:</label>
        <input type="text" id="join-code" name="join-code" />
        <button type="submit">Dołącz</button>
      </form>
    </div>
  );
}

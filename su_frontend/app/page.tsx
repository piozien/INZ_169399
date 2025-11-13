import Link from "next/link";

export default function HomePage() {
  return (
    <main>
      <h1>Portal Samorządu Szkolnego</h1>
      <p>Zaloguj się, aby zarządzać wydarzeniami, inicjatywami samorządu i propozycjami.</p>
      <ul>
        <li>
          <Link href="/login">Przejdź do logowania</Link>
        </li>
        <li>
          <Link href="/dashboard">Otwórz panel</Link>
        </li>
      </ul>
    </main>
  );
}

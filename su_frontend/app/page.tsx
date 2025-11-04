import Link from 'next/link';

export default function HomePage() {
  return (
    <main>
      <h1>Witaj w aplikacji Samorządu Studenckiego</h1>
      <p>
        <Link href="/login">
          Przejdź do logowania
        </Link>
      </p>
    </main>
  );
}

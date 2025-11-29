import Link from 'next/link';

export default function SuggestionsPage() {
  return (
    <section>
      <h1>Propozycje</h1>
      <p>Zobacz, co zaproponowała społeczność i śledź postępy.</p>
      <p>
        Chcesz podzielić się pomysłem?{' '}
        <Link href="/login">Zaloguj się, aby złożyć propozycję.</Link>
      </p>
    </section>
  );
}

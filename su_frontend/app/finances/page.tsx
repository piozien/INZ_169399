import Link from "next/link";

export default function FinancesPage() {
  return (
    <section>
      <h1>Finanse</h1>
      <p>Zarządzaj budżetem samorządu i klas.</p>
      <nav aria-label="Sekcje finansów">
        <ul>
          <li>
            <Link href="/finances/council">Finanse samorządowe</Link>
          </li>
          <li>
            <Link href="/finances/class">Finanse klasowe</Link>
          </li>
        </ul>
      </nav>
    </section>
  );
}


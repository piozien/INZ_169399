import Link from "next/link";

const exampleSuggestions = [
  {
    id: "cafe-upgrade",
    title: "Modernizacja stołówki studenckiej",
    summary:
      "Wprowadzenie zdrowszych opcji i większej liczby miejsc siedzących.",
  },
  {
    id: "study-rooms",
    title: "Przedłużenie godzin otwarcia sal do nauki",
    summary: "Umożliwienie dostępu w okresie egzaminów.",
  },
];

export default function SuggestionsPage() {
  return (
    <section>
      <h1>Propozycje</h1>
      <p>Zobacz, co zaproponowała społeczność i śledź postępy.</p>
      <ul>
        {exampleSuggestions.map((suggestion) => (
          <li key={suggestion.id}>
            <h2>{suggestion.title}</h2>
            <p>{suggestion.summary}</p>
          </li>
        ))}
      </ul>
      <p>
        Chcesz podzielić się pomysłem?{" "}
        <Link href="/login">Zaloguj się, aby złożyć propozycję.</Link>
      </p>
    </section>
  );
}

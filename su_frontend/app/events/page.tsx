const events = [
  {
    id: "open-day",
    name: "Dzień Otwarty",
    description: "Prezentacja inicjatyw samorządu i rekrutacja wolontariuszy.",
  },
  {
    id: "budget-review",
    name: "Spotkanie przeglądu budżetu",
    description: "Przegląd wydatków i planowanie nadchodzących kosztów.",
  },
];

export default function EventsPage() {
  return (
    <section>
      <h1>Wydarzenia</h1>
      <p>Przeglądaj nadchodzące i ostatnie aktywności samorządu szkolnego.</p>
      <ul>
        {events.map((event) => (
          <li key={event.id}>
            <h2>{event.name}</h2>
            <p>{event.description}</p>
          </li>
        ))}
      </ul>
    </section>
  );
}

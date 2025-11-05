"use client";

import Link from "next/link";

export default function LoginPage() {
  const handleMicrosoftLogin = () => {
    const oauth2Url = `${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/microsoft`;

    window.location.href = oauth2Url;
  };

  return (
    <main>
      <h1>Logowanie</h1>
      <p>Wybierz metodę logowania:</p>
      <button onClick={handleMicrosoftLogin}>
        Zaloguj się przez Microsoft
      </button>
      <br />
      <Link href="/">Wróć do strony głównej</Link>
    </main>
  );
}

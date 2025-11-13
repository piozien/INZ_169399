import type { Metadata } from "next";
import "./globals.css";
import Navbar from "@/components/Navbar";
import QueryClientWrapper from "./query-client-provider";

export const metadata: Metadata = {
  title: {
    default: "Portal Samorządu Szkolnego",
    template: "%s · Portal Samorządu Szkolnego",
  },
  description: "Portal samorządu szkolnego dla uczniów i nauczycieli.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pl" suppressHydrationWarning>
      <body>
        <QueryClientWrapper>
          <Navbar />
          <main>{children}</main>
        </QueryClientWrapper>
      </body>
    </html>
  );
}

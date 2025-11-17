"use client";

import { usePathname } from "next/navigation";
import Navbar from "./Navbar";

const SHOWN_PATHS = ["/dashboard"];

export default function ConditionalNavbar() {
  const pathname = usePathname();

  if (SHOWN_PATHS.includes(pathname)) {
    return <Navbar />;
  }

  return null;
}

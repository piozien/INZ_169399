export async function hashPassword(input: string): Promise<string> {
  if (typeof window === "undefined" || !window.crypto?.subtle) {
    return input;
  }

  const encoder = new TextEncoder();
  const data = encoder.encode(input);
  const digest = await window.crypto.subtle.digest("SHA-256", data);
  const hashArray = Array.from(new Uint8Array(digest));
  return hashArray.map((byte) => byte.toString(16).padStart(2, "0")).join("");
}


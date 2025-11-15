import { useQuery } from "@tanstack/react-query";
import { fetchCurrentUser } from "../api/user";

export function useCurrentUser() {
  return useQuery({
    queryKey: ["currentUser"],
    queryFn: fetchCurrentUser,
    retry: false,
    refetchOnWindowFocus: false,
  });
}


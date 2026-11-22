import { useState } from "react";
import { getErrorMessage } from "@/shared/api/errors";
import type { ViewState } from "@/shared/state/viewState";
import { useAuth } from "../context/AuthContext";

interface UseLoginFormOptions {
  onSuccess: () => void;
}

export function useLoginForm({ onSuccess }: UseLoginFormOptions) {
  const { signIn } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [viewState, setViewState] = useState<ViewState>("ready");
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    setViewState("loading");
    setError(null);
    try {
      await signIn(username, password);
      onSuccess();
    } catch (e) {
      setError(getErrorMessage(e, "Não foi possível entrar. Tente novamente."));
      setViewState("error");
    }
  };

  return { username, setUsername, password, setPassword, viewState, error, submit };
}

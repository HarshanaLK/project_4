"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api } from "@/lib/api";
import { useAuth } from "./AuthProvider";
import type { User } from "@/lib/types";

type AuthResponse = { token: string; user: User };

export function AuthForm({ mode }: { mode: "login" | "register" }) {
  const router = useRouter();
  const { setSession } = useAuth();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);

    const body = mode === "register"
      ? {
          displayName: form.get("displayName"),
          email: form.get("email"),
          password: form.get("password")
        }
      : { email: form.get("email"), password: form.get("password") };

    setBusy(true);
    setError("");

    try {
      const result = await api<AuthResponse>(`/api/auth/${mode}`, {
        method: "POST",
        body: JSON.stringify(body)
      });
      setSession(result.token, result.user);
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Authentication failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <form className="card auth-card" onSubmit={submit}>
        <Link className="brand" href="/"><span className="brand-dot" />Synapse Collab</Link>
        <h1 style={{ marginTop: 28 }}>{mode === "login" ? "Welcome back" : "Create your account"}</h1>
        <p className="muted">
          {mode === "login" ? "Open your collaborative workspaces." : "Start a real-time engineering workspace."}
        </p>

        {mode === "register" && (
          <div className="field">
            <label htmlFor="displayName">Display name</label>
            <input id="displayName" name="displayName" minLength={2} required />
          </div>
        )}

        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" name="email" type="email" required />
        </div>

        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" name="password" type="password" minLength={8} required />
        </div>

        {error && <p className="error">{error}</p>}
        <button className="btn primary" style={{ width: "100%", marginTop: 8 }} disabled={busy}>
          {busy ? "Working..." : mode === "login" ? "Sign in" : "Create account"}
        </button>

        <p className="muted" style={{ fontSize: 14, textAlign: "center", marginTop: 18 }}>
          {mode === "login" ? "New here? " : "Already registered? "}
          <Link href={mode === "login" ? "/register" : "/login"} style={{ color: "white" }}>
            {mode === "login" ? "Create account" : "Sign in"}
          </Link>
        </p>
      </form>
    </div>
  );
}

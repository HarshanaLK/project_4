import Link from "next/link";

export default function NotFound() {
  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <h1>Page not found</h1>
        <p className="muted">The collaboration route does not exist.</p>
        <Link className="btn primary" href="/dashboard">Back to dashboard</Link>
      </div>
    </div>
  );
}

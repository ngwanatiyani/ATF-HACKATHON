import { useEffect, useState } from "react";
import { call } from "./api.js";

const KIND = { DELAY: "Delay", QUEUE: "Long queue", DISRUPTION: "Disruption", CHANGE: "Route change", CLOSED: "Not running" };
const tone = label => label === "High" ? "#b4cc50" : label === "Medium" ? "#00a9f3" : "#ff6b5e";

export default function App() {
  const [user, setUser] = useState(sessionStorage.pulseName || "");
  const [board, setBoard] = useState({ routes: [], latest: [] });
  const [error, setError] = useState("");
  const [alert, setAlert] = useState("");
  const [busy, setBusy] = useState(false);
  const [brief, setBrief] = useState(null);
  const [checking, setChecking] = useState(false);
  const [api, setApi] = useState(localStorage.pulseApi || "");

  async function loadBoard(previous) {
    const next = await call("/api/board");
    setBoard(next);
    setError("");
    const newest = next.latest[0];
    if (newest && previous && newest.id !== previous) setAlert("New report on " + newest.route);
    return newest ? newest.id : previous;
  }

  useEffect(() => {
    let current = 0;
    let stop = false;
    async function tick() {
      try {
        const id = await loadBoard(current);
        if (!stop) current = id || 0;
      } catch (err) {
        if (!stop) setError(err.message);
      }
    }
    tick();
    const timer = setInterval(tick, 15000);
    return () => { stop = true; clearInterval(timer); };
  }, []);

  useEffect(() => {
    if (!sessionStorage.pulsePhone) return;
    call("/api/me").then(me => { setUser(me.name); sessionStorage.pulseName = me.name; }).catch(() => leave());
  }, []);

  function leave() {
    sessionStorage.clear();
    setUser("");
  }

  async function enter(event) {
    event.preventDefault();
    const form = event.target;
    sessionStorage.pulsePhone = form.phone.value.trim();
    sessionStorage.pulsePass = form.pass.value;
    try {
      const me = await call("/api/me");
      sessionStorage.pulseName = me.name;
      setUser(me.name);
      setError("");
    } catch (err) {
      sessionStorage.clear();
      setError(err.message);
    }
  }

  async function join(event) {
    event.preventDefault();
    const form = event.target;
    const name = form.name.value.trim();
    const phone = form.phone.value.trim();
    const password = form.pass.value;
    try {
      await call("/api/register", { name, phone, password });
      sessionStorage.pulsePhone = phone;
      sessionStorage.pulsePass = password;
      sessionStorage.pulseName = name;
      setUser(name);
      setError("");
    } catch (err) {
      setError(err.message);
    }
  }

  async function report(event) {
    event.preventDefault();
    const form = event.target;
    const minutes = form.minutes.value === "" ? null : Number(form.minutes.value);
    setBusy(true);
    try {
      await call("/api/reports", {
        route: form.route.value.trim(),
        kind: form.kind.value,
        note: form.note.value.trim(),
        minutes
      });
      form.note.value = "";
      form.minutes.value = "";
      await loadBoard(board.latest[0] ? board.latest[0].id : 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function checkRoad(route) {
    setChecking(true);
    try {
      setBrief(await call("/api/brief", { route }));
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setChecking(false);
    }
  }

  const routes = board.routes.slice().sort((a, b) => a.score - b.score);

  return (
    <>
      <nav>
        <a className="brand" href="#top">Route<span>Pulse</span></a>
        <a href="#report">Report</a>
        <a href="#scores">Routes</a>
        <a href="#live">Live</a>
        <a className="pill" href="#report">{user ? user : "Join"}</a>
      </nav>
      <main>
        <header className="hero" id="top">
          <div>
            <p className="eyebrow">Community transport intelligence</p>
            <h1>Know the rank before you leave.</h1>
            <p className="lede">A commuter marks a delay, a queue, or a route that changed. The next person reads it, and the route gets a reliability score.</p>
          </div>
          <figure className="shot">
            <img src="/images/buses.png" alt="Golden-hour buses on a wet Cape Town street, with Table Mountain behind them" />
            <figcaption>Cape Town, before the evening queue builds.</figcaption>
          </figure>
        </header>

        <section className="split" id="report">
          <figure className="shot cutout">
            <img src="/images/phone.png" alt="Two South African commuters looking at a phone together" />
            <figcaption>One report. The person behind you does not have to guess.</figcaption>
          </figure>
          <div className="glass">
            {alert && <p className="banner">{alert}</p>}
            {!user && (
              <div id="gate">
                <form onSubmit={enter}>
                  <div className="row">
                    <input name="phone" inputMode="tel" autoComplete="username" placeholder="Phone" required />
                    <input name="pass" type="password" autoComplete="current-password" placeholder="Password" required />
                  </div>
                  <button>Enter</button>
                </form>
                <p className="or">or join</p>
                <form onSubmit={join}>
                  <input name="name" autoComplete="name" placeholder="Your name" required />
                  <div className="row">
                    <input name="phone" inputMode="tel" placeholder="Phone" required />
                    <input name="pass" type="password" autoComplete="new-password" placeholder="Password" required />
                  </div>
                  <button>Join</button>
                </form>
              </div>
            )}
            {user && (
              <div>
                <p className="who">Reporting as {user}</p>
                <form onSubmit={report}>
                  <input name="route" list="route-names" placeholder="Route, e.g. Khayelitsha – Cape Town CBD" required />
                  <datalist id="route-names">
                    {routes.map(route => <option key={route.name} value={route.name} />)}
                  </datalist>
                  <div className="row">
                    <select name="kind" defaultValue="DELAY">
                      <option value="DELAY">Taxi or bus delay</option>
                      <option value="QUEUE">Long queue</option>
                      <option value="DISRUPTION">Accident or closure</option>
                      <option value="CHANGE">Route changed</option>
                      <option value="CLOSED">Not running</option>
                    </select>
                    <input name="minutes" inputMode="numeric" placeholder="Wait (minutes)" />
                  </div>
                  <input name="note" placeholder="What should the next person know?" maxLength={160} />
                  <div className="row">
                    <button disabled={busy}>Report this route</button>
                    <button className="ghost" type="button" disabled={checking} onClick={event => {
                      const route = event.target.form.route.value.trim();
                      if (!route) { setError("Type the route first"); return; }
                      checkRoad(route);
                    }}>{checking ? "Checking the road…" : "Check the road"}</button>
                  </div>
                </form>
                {brief && (
                  <article className="brief">
                    <b>{brief.status}</b>
                    <p>{brief.summary}</p>
                    <ul>
                      {(brief.sources || []).map(source => (
                        <li key={source.url}><a href={source.url} target="_blank" rel="noreferrer">{source.title}</a></li>
                      ))}
                    </ul>
                  </article>
                )}
                <button className="ghost" type="button" onClick={leave}>Leave</button>
              </div>
            )}
            <p className="err">{error}</p>
          </div>
        </section>

        <section id="scores">
          <div className="split head">
            <div>
              <h2>Route reliability</h2>
              <p className="lede">The score starts at 100. The five newest reports pull it down: a delay costs 20, a queue 15, a disruption 35, a change 30, and a service that is not running costs 50.</p>
            </div>
            <figure className="shot cutout short">
              <img src="/images/commuters.png" alt="Four South African commuters, one of them checking a phone" />
            </figure>
          </div>
          <div className="cards">
            {routes.length === 0 && <p className="muted">No routes yet. Report the one you are standing in.</p>}
            {routes.map(route => (
              <article className="card" key={route.name}>
                <div>
                  <b>{route.name}</b>
                  <small>{route.reports} report{route.reports === 1 ? "" : "s"}{route.avgWait == null ? "" : " · avg wait " + route.avgWait + " min"}</small>
                </div>
                <div className="score" style={{ color: tone(route.label) }}>{route.score}<small>{route.label}</small></div>
              </article>
            ))}
          </div>
        </section>

        <section id="live">
          <h2>Latest reports</h2>
          <ul className="glass feed">
            {board.latest.length === 0 && <li className="muted">No reports yet.</li>}
            {board.latest.map(hit => (
              <li key={hit.id}>
                <span className="k">{KIND[hit.kind] || hit.kind}</span> · {hit.route}
                <small>{hit.by} · {new Intl.DateTimeFormat("en-ZA", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(hit.at))}{hit.minutes == null ? "" : " · " + hit.minutes + " min"}{hit.note ? " · " + hit.note : ""}</small>
              </li>
            ))}
          </ul>
        </section>

        <label className="server">API
          <input value={api} spellCheck={false} placeholder="Leave empty in dev. On Vercel, paste the API origin." onChange={event => { const value = event.target.value.trim(); setApi(event.target.value); localStorage.pulseApi = value; }} />
        </label>
      </main>
    </>
  );
}

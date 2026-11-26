import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import api from './api';
import mountain from './assets/mountain.png';
import logo from './assets/brand-logo.png';

const inputClass =
  'w-full rounded-lg border border-stone-300 bg-white px-3.5 py-3 text-stone-800 placeholder:text-stone-400 outline-none transition focus:border-green-700 focus:ring-2 focus:ring-green-700/20';

const roles = [
  { id: 'CITIZEN', icon: '🤝', title: 'Citizen', text: 'Ask for help or volunteer' },
  { id: 'NGO', icon: '🏢', title: 'NGO', text: 'Run campaigns and relief work' },
];

const demoAccounts = [
  { email: 'admin@reliefbridge.com', pass: 'Admin@12345678', label: 'admin' },
  { email: 'ngo.test@example.com', pass: 'Admin@12345678', label: 'ngo' },
  { email: 'amit.test@example.com', pass: 'Admin@12345678', label: 'citizen' },
];

function Field({ label, children }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-stone-700">{label}</span>
      {children}
    </label>
  );
}

function PasswordInput({ value, onChange, placeholder = '••••••••' }) {
  const [show, setShow] = useState(false);
  return (
    <div className="relative">
      <input
        type={show ? 'text' : 'password'}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className={`${inputClass} pr-16`}
      />
      <button
        type="button"
        onClick={() => setShow(!show)}
        className="absolute inset-y-0 right-3 text-sm font-medium text-green-800 hover:underline"
      >
        {show ? 'Hide' : 'Show'}
      </button>
    </div>
  );
}

export default function LoginRegister() {
  const [mode, setMode] = useState('login');
  const [role, setRole] = useState('CITIZEN');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [skills, setSkills] = useState('');
  const [regNo, setRegNo] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await api.post('/auth/login', { email, password });
      login(
        {
          name: res.name || email.split('@')[0],
          email: res.email,
          role: res.role,
          id: res.userId,
          isDonor: res.isDonor,
          isVolunteer: res.isVolunteer,
        },
        res.token
      );
      if (res.role === 'NGO' || res.role === 'ADMIN') {
        navigate('/ngo');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (regPassword.length < 12) {
      setError('Password must contain at least 12 characters.');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        name: fullName.trim(),
        email: regEmail.trim(),
        password: regPassword,
        role: role,
        isDonor: role === 'CITIZEN',
        isVolunteer: role === 'CITIZEN' && Boolean(skills.trim()),
        skills: role === 'CITIZEN' && skills.trim() ? skills.trim() : null,
        orgName: role === 'NGO' ? fullName.trim() : null,
        regNo: role === 'NGO' ? (regNo.trim() || 'REG-' + Date.now()) : null,
      };

      await api.post('/auth/register', payload);

      setSuccess(
        role === 'NGO'
          ? 'NGO registration submitted! Account is pending admin approval.'
          : 'Registration successful! Please log in with your credentials.'
      );
      setMode('login');
      setEmail(regEmail);
      setPassword(regPassword);
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen bg-[#F4F8F3]">
      {/* Left: nature panel (hidden on mobile) */}
      <aside
        className="relative hidden w-1/2 flex-col justify-between bg-cover bg-center p-12 text-white lg:flex"
        style={{ backgroundImage: `url(${mountain})` }}
      >
        <div className="absolute inset-0 bg-gradient-to-b from-green-950/70 via-green-900/55 to-green-950/85" />
        <div className="relative flex items-center gap-3">
          <img src={logo} alt="" className="h-10 w-10 rounded-lg bg-white/90 object-contain p-1" />
          <span className="text-xl font-bold tracking-tight">ReliefBridge</span>
        </div>
        <div className="relative max-w-md">
          <h1 className="text-4xl font-bold leading-tight">Together, we rebuild.</h1>
          <p className="mt-4 text-lg text-green-50/90">
            Connect donors, volunteers and NGOs so help reaches disaster-hit families faster, with every donation tracked.
          </p>
        </div>
        <p className="relative text-sm text-green-100/80">Service to man is service to God.</p>
      </aside>

      {/* Right: one card, two modes */}
      <main className="flex w-full items-center justify-center p-6 lg:w-1/2">
        <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl shadow-green-900/10">
          {/* Mobile brand */}
          <div className="mb-6 flex items-center justify-center gap-2 lg:hidden">
            <img src={logo} alt="" className="h-9 w-9 rounded-lg bg-green-50 object-contain p-1" />
            <span className="text-lg font-bold text-green-950">ReliefBridge</span>
          </div>

          {/* Mode switch */}
          <div className="mb-8 grid grid-cols-2 gap-1 rounded-xl bg-stone-100 p-1 text-sm font-semibold">
            <button
              type="button"
              onClick={() => { setMode('login'); setError(''); }}
              className={`rounded-lg py-2 transition ${
                mode === 'login' ? 'bg-white text-green-900 shadow-sm' : 'text-stone-500 hover:text-stone-800'
              }`}
            >
              Log In
            </button>
            <button
              type="button"
              onClick={() => { setMode('register'); setError(''); }}
              className={`rounded-lg py-2 transition ${
                mode === 'register' ? 'bg-white text-green-900 shadow-sm' : 'text-stone-500 hover:text-stone-800'
              }`}
            >
              Sign Up
            </button>
          </div>

          {error && (
            <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700 border border-red-200">
              {error}
            </div>
          )}

          {success && (
            <div className="mb-4 rounded-lg bg-green-50 p-3 text-sm text-green-800 border border-green-200">
              {success}
            </div>
          )}

          {mode === 'login' ? (
            <>
              <h2 className="text-2xl font-bold text-green-900">Welcome back</h2>
              <p className="mb-6 mt-1 text-stone-500">Sign in to coordinate and deliver relief.</p>

              <form onSubmit={handleLogin} className="space-y-4">
                <Field label="Email address">
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    className={inputClass}
                  />
                </Field>
                <Field label="Password">
                  <PasswordInput value={password} onChange={(e) => setPassword(e.target.value)} />
                </Field>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full rounded-lg bg-green-800 py-3 font-bold text-white transition hover:bg-green-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-green-700 focus-visible:ring-offset-2 disabled:opacity-60"
                >
                  {loading ? 'Logging in...' : 'Log in'}
                </button>
              </form>

              <div className="mt-6 rounded-lg bg-green-50 p-3 text-sm text-stone-600">
                <p className="mb-2 font-medium text-green-900">Demo accounts (click to fill)</p>
                <div className="flex flex-wrap gap-2">
                  {demoAccounts.map((a) => (
                    <button
                      key={a.label}
                      type="button"
                      onClick={() => {
                        setEmail(a.email);
                        setPassword(a.pass);
                      }}
                      className="rounded-full border border-green-200 bg-white px-3 py-1 text-xs text-green-900 hover:bg-green-100"
                    >
                      {a.label}
                    </button>
                  ))}
                </div>
              </div>
            </>
          ) : (
            <>
              <h2 className="text-2xl font-bold text-green-900">Join ReliefBridge</h2>
              <p className="mb-6 mt-1 text-stone-500">Create an account and start helping.</p>

              <form onSubmit={handleRegister} className="space-y-4">
                <div>
                  <span className="mb-2 block text-sm font-medium text-stone-700">I am registering as a</span>
                  <div className="grid grid-cols-2 gap-3">
                    {roles.map((r) => (
                      <button
                        key={r.id}
                        type="button"
                        onClick={() => setRole(r.id)}
                        className={`rounded-xl border p-3 text-left transition ${
                          role === r.id
                            ? 'border-green-700 bg-green-50 ring-2 ring-green-700/20'
                            : 'border-stone-300 hover:border-green-600'
                        }`}
                      >
                        <span className="text-2xl">{r.icon}</span>
                        <span className="mt-1 block font-semibold text-stone-800">{r.title}</span>
                        <span className="block text-xs text-stone-500">{r.text}</span>
                      </button>
                    ))}
                  </div>
                </div>

                <Field label={role === 'NGO' ? 'Organization name *' : 'Full name *'}>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder={role === 'NGO' ? 'Your organization' : 'Your full name'}
                    className={inputClass}
                  />
                </Field>

                {role === 'NGO' && (
                  <Field label="Registration number *">
                    <input
                      type="text"
                      required
                      value={regNo}
                      onChange={(e) => setRegNo(e.target.value)}
                      placeholder="e.g. NGO-MH-2024-001"
                      className={inputClass}
                    />
                  </Field>
                )}

                <Field label="Email address *">
                  <input
                    type="email"
                    required
                    value={regEmail}
                    onChange={(e) => setRegEmail(e.target.value)}
                    placeholder="you@example.com"
                    className={inputClass}
                  />
                </Field>

                <Field label="Password * (min 12 characters)">
                  <PasswordInput value={regPassword} onChange={(e) => setRegPassword(e.target.value)} />
                </Field>

                {role === 'CITIZEN' && (
                  <Field label="Volunteer skills (e.g. First Aid, Driving)">
                    <input
                      type="text"
                      value={skills}
                      onChange={(e) => setSkills(e.target.value)}
                      placeholder="e.g. first aid, driving, cooking"
                      className={inputClass}
                    />
                  </Field>
                )}

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full rounded-lg bg-green-800 py-3 font-bold text-white transition hover:bg-green-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-green-700 focus-visible:ring-offset-2 disabled:opacity-60"
                >
                  {loading ? 'Creating account...' : 'Create account'}
                </button>
              </form>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

/**
 * @file LoginPage.jsx
 * @description Página de autenticação do SyncCarreira.
 *
 * Permite que psicólogas e alunos já cadastrados acessem a plataforma
 * informando e-mail e senha. Após autenticação bem-sucedida, o usuário
 * é redirecionado para /home.
 *
 * Dependências:
 *  - AuthContext: fornece a função login() e o estado loading
 *  - React Router: useNavigate para redirecionamento, Link para /cadastro
 *  - Footer: componente de rodapé reutilizável
 */

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import Footer from '../../components/Footer/Footer.jsx'
import './LoginPage.css'

/**
 * LoginPage
 * Página de login com campos de e-mail, senha e opção "Lembrar de mim".
 *
 * @returns {JSX.Element}
 */
export default function LoginPage() {
  const { login, loading } = useAuth()
  const navigate = useNavigate()

  // ── Estados do formulário ────────────────────────────────────
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [lembrar, setLembrar]   = useState(false)
  const [showPass, setShowPass] = useState(false)
  const [error, setError]       = useState('')

  // ── Submissão do formulário ──────────────────────────────────

  /**
   * Valida os campos, chama o AuthContext e redireciona em caso de sucesso.
   *
   * @param {React.FormEvent} e
   */
  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    if (!email || !password) {
      setError('Preencha todos os campos.')
      return
    }

    const result = await login(email, password)

    if (result.success) {
      navigate('/home')
    } else {
      setError(result.message)
    }
  }

  // ── Renderização ─────────────────────────────────────────────
  return (
    <div className="lp-root">

      {/* ── Cabeçalho ── */}
      <header className="lp-header">
        <span className="lp-header__brand">SyncCarreira</span>
        <button className="lp-header__help" aria-label="Ajuda">?</button>
      </header>

      {/* ── Conteúdo principal ── */}
      <main className="lp-main">
        <div className="lp-card">

          {/* Ícone ilustrativo */}
          <div className="lp-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" width="22" height="22">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>

          <h1 className="lp-card__title">Bem-vindo de volta</h1>
          <p className="lp-card__sub">Acesse sua jornada profissional personalizada.</p>

          <form onSubmit={handleSubmit} noValidate>

            {/* Área de feedback de erro */}
            {error && (
              <div className="lp-error" role="alert" aria-live="polite">
                {error}
              </div>
            )}

            {/* Campo e-mail */}
            <div className="lp-field">
              <label htmlFor="email">EMAIL</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="seu@email.com"
                autoComplete="email"
              />
            </div>

            {/* Campo senha com toggle de visibilidade */}
            <div className="lp-field">
              <label htmlFor="password">SENHA</label>
              <div className="lp-field__wrap">
                <input
                  id="password"
                  type={showPass ? 'text' : 'password'}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="••••••••"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="lp-field__eye"
                  onClick={() => setShowPass(!showPass)}
                  aria-label={showPass ? 'Ocultar senha' : 'Mostrar senha'}
                >
                  {showPass
                    ? <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="17" height="17"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                    : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="17" height="17"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  }
                </button>
              </div>
            </div>

            {/* Linha: lembrar de mim + esqueceu a senha */}
            <div className="lp-row">
              <label className="lp-check">
                <input
                  type="checkbox"
                  checked={lembrar}
                  onChange={e => setLembrar(e.target.checked)}
                />
                <span>Lembrar de mim</span>
              </label>
              {/* TODO: implementar fluxo de recuperação de senha */}
              <button type="button" className="lp-link">Esqueceu a senha?</button>
            </div>

            {/* Botão de submit com estado de loading */}
            <button type="submit" className="lp-btn" disabled={loading}>
              {loading ? <span className="lp-spinner" aria-label="Carregando" /> : 'Entrar'}
            </button>
          </form>

          {/* Link para cadastro */}
          <p className="lp-footer-text">
            Não tem uma conta?{' '}
            <Link to="/cadastro" className="lp-link">Criar conta</Link>
          </p>
        </div>

        {/* Banner promocional */}
        <div className="lp-banner" aria-hidden="true">
          <p>JUNTE-SE A +10.000 ESTUDANTES</p>
        </div>
      </main>

      {/* Rodapé modular reutilizável */}
      <Footer />
    </div>
  )
}

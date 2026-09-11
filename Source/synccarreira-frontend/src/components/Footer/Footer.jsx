/**
 * @file Footer.jsx
 * @description Componente de rodapé reutilizável do SyncCarreira.
 *
 * Substitui os footers duplicados nas páginas de Login e Cadastro.
 * Para usar em uma página, basta importar e renderizar:
 *
 * @example
 * import Footer from '../../components/Footer/Footer.jsx'
 * // dentro do JSX:
 * <Footer />
 */

import './Footer.css'

/**
 * Footer
 * Rodapé padrão com copyright e links institucionais.
 * Não recebe props — o conteúdo é fixo e centralizado aqui.
 *
 * @returns {JSX.Element}
 */
export default function Footer() {
  return (
    <footer className="sc-footer">
      <span className="sc-footer__copy">
        © {new Date().getFullYear()} SyncCarreira. Todos os direitos reservados.
      </span>

      <nav className="sc-footer__links" aria-label="Links institucionais">
        {/* TODO: substituir os botões por <Link> quando as páginas existirem */}
        <button type="button">Privacidade</button>
        <button type="button">Termos de Uso</button>
        <button type="button">Suporte</button>
      </nav>
    </footer>
  )
}

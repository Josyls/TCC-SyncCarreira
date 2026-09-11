import { AuthProvider } from './context/AuthContext.jsx'
import AppRoutes from './routes/AppRoutes2.jsx'

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

export default App

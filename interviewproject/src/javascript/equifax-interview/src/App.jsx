import { useState } from 'react'
import './App.css'
import UserProfiles from './UserProfiles';

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="container mx-auto">
      <UserProfiles />
    </div>
  )
}

export default App

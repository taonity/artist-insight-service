'use client'

import KoFiButton from "../../components/KoFiButton"
import Header from "@/components/Header"
import { useUser } from "@/hooks/useUser"
import ErrorNotification from '@/components/ErrorNotification'
import Loading from '@/components/Loading'

import { useEffect, useState } from 'react'


export default function Donate() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const user = useUser(setErrorMessage);

  if (!user) return <Loading />


  return (
    <div>
      {errorMessage && (
        <ErrorNotification message={errorMessage} onClose={() => setErrorMessage(null)} />
      )}
      <Header user={user}/>
      <div className="donation-instructions">
        SOme donation info and instructions
      </div>
      <div className="user-id-box">
        {user.privateUserObject.id}
      </div>
      <div>
        <KoFiButton username="N4N11KVW3E" label="Support me on Ko-fi" />
      </div>
    </div>
  )
}

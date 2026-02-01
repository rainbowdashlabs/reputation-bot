import { ref, readonly } from 'vue'
import type { GuildSessionPOJO } from '@/api/types'

const session = ref<GuildSessionPOJO | null>(null)

export function useSession() {
  const setSession = (data: GuildSessionPOJO) => {
    session.value = data
  }

  const clearSession = () => {
    session.value = null
  }

  return {
    session: readonly(session),
    setSession,
    clearSession
  }
}

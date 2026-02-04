import { defineStore } from 'pinia'
import type { ApiErrorResponse } from '../api/types'

export interface ErrorItem {
  id: string
  error: ApiErrorResponse
  timestamp: number
}

export const useErrorStore = defineStore('error', {
  state: () => ({
    errors: [] as ErrorItem[]
  }),

  actions: {
    addError(error: ApiErrorResponse) {
      const errorItem: ErrorItem = {
        id: `error-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
        error,
        timestamp: Date.now()
      }
      this.errors.push(errorItem)
    },

    removeError(id: string) {
      const index = this.errors.findIndex(e => e.id === id)
      if (index !== -1) {
        this.errors.splice(index, 1)
      }
    },

    clearAllErrors() {
      this.errors = []
    }
  }
})

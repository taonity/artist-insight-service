export type AdvisorySeverity = 'INFO' | 'WARNING' | 'ERROR'

export interface Advisory {
  code: string
  title: string
  detail: string
  severity: AdvisorySeverity
}
export interface UserImage {
  url: string
}

export interface PrivateUserObject {
  id: string
  displayName: string
  images?: UserImage[]
}

export interface User {
  privateUserObject: PrivateUserObject
  gptUsagesLeft: number
}
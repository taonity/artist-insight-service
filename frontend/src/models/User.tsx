type PrivateUserObject = {
    id: string,
    displayName: string,
    images?: { url: string }[]
}

type User = {
    privateUserObject: PrivateUserObject,
    gptUsagesLeft: number
}

export default User
type PrivateUserObject = {
    displayName: string,
    images?: { url: string }[]
}

type User = {
    privateUserObject: PrivateUserObject,
    gptUsagesLeft: number
}

export default User
import { create } from 'zustand';

export interface User {
    id: string;
    name: string;
    role: 'admin' | 'responder' | 'volunteer' | 'victim';
    email: string;
}

interface UserState {
    users: User[];
    addUser: (user: User) => void;
    setUsers: (users: User[]) => void;
}

export const useUserStore = create<UserState>((set) => ({
    users: [],
    addUser: (user) => set((state) => ({ users: [...state.users, user] })),
    setUsers: (users) => set({ users }),
}));

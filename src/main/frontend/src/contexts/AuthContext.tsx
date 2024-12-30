import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

interface User {
    id: string;
    name: string;
    email: string;
    picture?: string;
    roles: string[];
}

interface AuthContextType {
    user: User | null;
    loading: boolean;
    error: string | null;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
    user: null,
    loading: true,
    error: null,
    logout: () => {}
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchUser();
    }, []);

    const fetchUser = async () => {
        try {
            const response = await axios.get('/api/auth/user');
            setUser(response.data);
        } catch (err) {
            setError('Failed to fetch user');
            console.error('Auth error:', err);
        } finally {
            setLoading(false);
        }
    };

    const logout = async () => {
        try {
            const response = await axios.post('/api/auth/logout');
            // Redirect to Zitadel's logout URL
            if (response.data.logoutUrl) {
                window.location.href = response.data.logoutUrl;
            }
        } catch (err) {
            console.error('Logout error:', err);
            // Fallback to home page if logout fails
            window.location.href = '/';
        }
    };

    return (
        <AuthContext.Provider value={{ user, loading, error, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);

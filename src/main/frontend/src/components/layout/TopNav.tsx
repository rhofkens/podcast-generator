import { useAuth } from '../../contexts/AuthContext';
import { Button } from '../ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';

export function TopNav() {
  const { user, logout } = useAuth();

  return (
    <header className="h-14 border-b flex items-center px-6 bg-white">
      <div className="flex-1" />
      <div className="flex items-center gap-4">
        {user ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="flex items-center gap-2 p-2 rounded-full hover:bg-gray-100">
                <span className="w-8 h-8 rounded-full bg-gray-200">
                  {user.picture ? (
                    <img src={user.picture} alt={user.name} className="w-full h-full rounded-full" />
                  ) : (
                    <span className="flex items-center justify-center w-full h-full text-sm font-medium">
                      {user.name?.charAt(0)}
                    </span>
                  )}
                </span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem className="font-medium">{user.name}</DropdownMenuItem>
              <DropdownMenuItem onClick={logout}>Logout</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : (
          <Button onClick={() => window.location.href = '/login'}>
            Sign in
          </Button>
        )}
      </div>
    </header>
  );
}


import React, {
  useState,
  useEffect,
} from 'react';
import LoginScreen from '../screens/login_screen';
import LoadingScreen from '../screens/loading_screen';

const context = {};

export const UserContext = React.createContext(
  context
);

export interface UserProviderProps {
  children: React.ReactNode;
}

function UserProvider(props: UserProviderProps): JSX.Element {
  const [getStateIsLoading, setStateIsLoading] = useState(true);
  const [getStateUser, setStateUser] = useState({});
  const [getStateCredentials, setStateCredentials] = useState({});
  const [getStateIsLoggedIn, setStateIsLoggedIn] = useState({});
  const {
    children,
  } = props;
  function login(username: string, password: string): Promise<boolean> {
    return false;
  }
  function logout(): Promise<void> {
    return false;
  }
  useEffect(() => {
    setStateIsLoading(false);
  }, []);
  return (
    <UserContext.Provider value={
      {
        login,
        logout,
        isLoggedIn: getStateIsLoggedIn,
      }
    }>
      {getStateIsLoading ? <LoadingScreen /> : (getStateIsLoggedIn ? children : <LoginScreen />)}
    </UserContext.Provider>
  );
}

export default UserProvider;

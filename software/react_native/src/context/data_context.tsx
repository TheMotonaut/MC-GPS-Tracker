
import React, {
  useState,
  useEffect,
} from 'react';
import LoadingScreen from '../screens/loading_screen';

const context = {};

export const DataContext = React.createContext(
  context
);

export interface DataProviderProps {
  children: React.ReactNode;
}

function DataProvider(props: DataProviderProps): JSX.Element {
  const [getStateIsLoading, setStateIsLoading] = useState(true);
  const [getStateVehicles, setStateVehicles] = useState([]);
  const {
    children,
  } = props;
  useEffect(() => {
    setStateIsLoading(false);
  }, []);
  return (
    <DataContext.Provider value={
      {
        vehicles: getStateVehicles,
      }
    }>
      {getStateIsLoading ? <LoadingScreen /> : children}
    </DataContext.Provider>
  );
}

export default DataProvider;

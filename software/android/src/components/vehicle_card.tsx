
import React, {
  useContext,
} from 'react';
import {
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { themes } from '../styles/theme';
import { VehicleState } from '../data/state';
import {
  LanguageContext,
} from '../context/language_context';

interface VehicleCardProps {
  registrationNumber: string;
  gpsCoordinates: [number, number];
  altitude: number;
  state: VehicleState;
}

function VehicleCard(props: VehicleCardProps): JSX.Element {
  const {
    registrationNumber,
    gpsCoordinates,
    altitude,
    state
  } = props;
  const {
    translations,
  } = useContext(LanguageContext);
  const {
    vehicleCard,
  } = translations;
  return (
    <View style={styles.card}>
      <View>
        <Text>
          {registrationNumber}
        </Text>
        <View>
          <Text>
            {vehicleCard.state}
          </Text>
          <Text>
            {state}
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: themes.light.background.highlighted,
  }
});

export default VehicleCard;

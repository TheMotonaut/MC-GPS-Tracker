
import React, {
  useContext,
} from 'react';
import {
  StyleSheet,
  ScrollView,
} from 'react-native';

import { themes } from '../styles/theme';
import { VehicleState } from '../data/state';
import {
  LanguageContext,
} from '../context/language_context';
import VehicleCard from '../components/vehicle_card';

function MainScreen(): JSX.Element {
  const {
    translations,
  } = useContext(LanguageContext);
  return (
    <ScrollView style={styles.background}>
      <VehicleCard
        registrationNumber = {"ABC 123"}
        gpsCoordinates = {[12.32, 56.23]}
        altitude = {72.23}
        state = {VehicleState.STATIONARY}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  background: {
    backgroundColor: themes.light.background.normal,
  }
});

export default MainScreen;

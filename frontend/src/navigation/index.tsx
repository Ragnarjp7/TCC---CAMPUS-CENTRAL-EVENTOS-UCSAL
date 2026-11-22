import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import type { ComponentType } from "react";
import { FloatingTabBar } from "@/shared/components/FloatingTabBar";
import PlaceholderScreen from "@/shared/components/PlaceholderScreen";
import type { RootStackParamList, TabParamList } from "./types";

import SplashScreen from "@/features/onboarding/screens/SplashScreen";
import LoginScreen from "@/features/auth/screens/LoginScreen";
import ProfileScreen from "@/features/auth/screens/ProfileScreen";
import HomeScreen from "@/features/feed/screens/HomeScreen";
import EventsScreen from "@/features/events/screens/EventsScreen";
import EventDetailScreen from "@/features/events/screens/EventDetailScreen";

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<TabParamList>();

function MainTabs() {
  return (
    <Tab.Navigator tabBar={(props) => <FloatingTabBar {...props} />} screenOptions={{ headerShown: false }}>
      <Tab.Screen name="Início" component={HomeScreen} />
      <Tab.Screen name="Perfil" component={ProfileScreen} />
      <Tab.Screen name="Notificações" component={PlaceholderScreen as ComponentType} />
    </Tab.Navigator>
  );
}

export default function RootNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Splash" screenOptions={{ headerShown: false, animation: "fade" }}>
        <Stack.Screen name="Splash" component={SplashScreen} />
        <Stack.Screen name="Main" component={MainTabs} />
        <Stack.Screen name="Login" component={LoginScreen} options={{ animation: "slide_from_bottom" }} />
        <Stack.Screen name="Events" component={EventsScreen} options={{ animation: "slide_from_right" }} />
        <Stack.Screen name="EventDetail" component={EventDetailScreen} options={{ animation: "slide_from_right" }} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

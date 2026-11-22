import { useState } from "react";
import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Button, Dots, ImgSlot } from "@/shared/components/ui";
import type { RootScreenProps } from "@/navigation/types";
import { ONBOARDING } from "../data/slides";

export default function OnboardingScreen({ navigation }: RootScreenProps<"Onboarding">) {
  const [step, setStep] = useState(0);
  const { title, text } = ONBOARDING[step];
  const last = step === ONBOARDING.length - 1;

  return (
    <SafeAreaView testID="onboarding-screen" className="flex-1 bg-white">
      <View className="flex-1 px-6 pb-8 pt-10">
        <ImgSlot testID="onboarding-image" className="aspect-square w-full max-w-72 self-center rounded-3xl" />
        <View className="mt-8 flex-1 items-center">
          <Text testID="onboarding-title" className="text-center text-2xl font-extrabold leading-tight text-brand">{title}</Text>
          <Text testID="onboarding-text" className="mt-3 max-w-64 text-center text-sm leading-relaxed text-slate-500">{text}</Text>
        </View>
        <Dots testID="onboarding-dots" total={ONBOARDING.length} active={step} />
        <Button
          testID="onboarding-next-button"
          className="mt-6"
          title={last ? "Começar" : "Próximo"}
          onPress={() => (last ? navigation.replace("Main") : setStep(step + 1))}
        />
      </View>
    </SafeAreaView>
  );
}

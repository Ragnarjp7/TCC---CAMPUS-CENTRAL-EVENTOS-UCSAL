import { Text, View } from "react-native";
import { ImgSlot } from "@/shared/components/ui";
import type { GroupMember } from "../types";

interface MemberCardProps {
  member: GroupMember;
  /** Mostra o nome do grupo junto do cargo (quando o usuário participa de mais de um). */
  showGroup?: boolean;
}

export const MemberCard = ({ member, showGroup = false }: MemberCardProps) => {
  const role = member.roleName ?? "Ligante";
  return (
    <View testID={`member-card-${member.id}`} className="flex-row items-center gap-3 rounded-2xl bg-white p-4 shadow-sm">
      <ImgSlot testID={`member-card-${member.id}-avatar`} className="size-10 rounded-full" />
      <View className="flex-1">
        <Text testID={`member-card-${member.id}-name`} numberOfLines={1} className="text-sm font-bold text-slate-900">
          {member.name}
        </Text>
        <Text testID={`member-card-${member.id}-role`} numberOfLines={1} className="text-xs text-slate-400">
          {showGroup ? `${role} · ${member.groupName}` : role}
        </Text>
      </View>
    </View>
  );
};

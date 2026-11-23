import { Pressable, Text, View } from "react-native";
import { Lock } from "lucide-react-native";
import { ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import { timeAgo } from "@/shared/utils/date";
import { QuickInfoButton, QuickInfoPanel, useQuickInfoDisclosure } from "@/features/events/components/QuickInfo";
import { useEvent } from "@/features/events/hooks/useEvent";
import type { Post } from "../types";

interface PostCardProps {
  post: Post;
  /** Ausente quando o post não tem evento ligado: o card fica sem ação e sem o botão. */
  onPress?: () => void;
}

export const PostCard = ({ post, onPress }: PostCardProps) => {
  const disclosure = useQuickInfoDisclosure();
  const eventQuery = useEvent(post.eventId ?? null, { enabled: disclosure.expanded });

  return (
    <Pressable
      testID={`post-card-${post.id}`}
      accessibilityRole={onPress ? "button" : undefined}
      accessibilityLabel={`${post.title}, publicado por ${post.author}.${onPress ? " Ver detalhes" : ""}`}
      onPress={onPress}
      disabled={!onPress}
      className="gap-3 rounded-2xl bg-white p-4 shadow-sm active:opacity-90"
    >
      <View testID={`post-card-${post.id}-header`} className="flex-row items-center gap-3">
        <ImgSlot testID={`post-card-${post.id}-avatar`} className="size-9 rounded-full" />
        <View className="flex-1">
          <Text testID={`post-card-${post.id}-author`} numberOfLines={1} className="text-sm font-bold text-slate-900">{post.author}</Text>
          <Text testID={`post-card-${post.id}-meta`} className="text-[11px] text-slate-400">{post.category} · {timeAgo(post.createdAt)}</Text>
        </View>
        {post.visibility === "private" && <Lock testID={`post-card-${post.id}-private-icon`} size={14} color={colors.brand} />}
      </View>
      <ImgSlot testID={`post-card-${post.id}-cover`} className="h-32 w-full rounded-lg bg-slate-800">
        <Text testID={`post-card-${post.id}-title`} className="text-base font-extrabold text-white">{post.title}</Text>
      </ImgSlot>
      <Text testID={`post-card-${post.id}-body`} numberOfLines={4} className="text-xs leading-relaxed text-slate-600">{post.body}</Text>
      {onPress && (
        // sem gap aqui: o espaçamento fica dentro da área colapsável, então some junto quando fechada
        <View>
          <QuickInfoPanel
            testID={`post-card-${post.id}-quick-info`}
            disclosure={disclosure}
            event={eventQuery.data}
            isError={eventQuery.isError}
          />
          <QuickInfoButton testID={`post-card-${post.id}-details-button`} disclosure={disclosure} />
        </View>
      )}
    </Pressable>
  );
};

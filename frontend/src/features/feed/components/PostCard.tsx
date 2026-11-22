import { Pressable, Text, View } from "react-native";
import { Lock } from "lucide-react-native";
import { ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import { timeAgo } from "@/shared/utils/date";
import type { Post } from "../types";

interface PostCardProps {
  post: Post;
  onPress: () => void;
}

export const PostCard = ({ post, onPress }: PostCardProps) => (
  <View testID={`post-card-${post.id}`} className="gap-3 rounded-2xl bg-white p-4 shadow-sm">
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
    <Pressable
      testID={`post-card-${post.id}-details-button`}
      accessibilityRole="button"
      onPress={onPress}
      className="h-9 items-center justify-center self-start rounded-lg bg-brand px-4 active:opacity-80"
    >
      <Text className="text-xs font-semibold text-white">Ver Detalhes</Text>
    </Pressable>
  </View>
);

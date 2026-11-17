export type NotificationItemView = {
  id: string;
  title: string;
  body: string;
  deepLink: string | null;
  read: boolean;
  createdAt: string | null;
  eventType: string | null;
};

export class PodcastGenerationWebSocket {
    private ws: WebSocket | null = null;
    private messageHandlers: ((data: any) => void)[] = [];

    constructor(podcastId: string) {
        // Use window.location to dynamically build the WebSocket URL
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/api/ws/podcast-generation/${podcastId}`;
        
        this.ws = new WebSocket(wsUrl);
        
        this.ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            this.messageHandlers.forEach(handler => handler(data));
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };
    }

    public onMessage(handler: (data: any) => void) {
        this.messageHandlers.push(handler);
    }

    public close() {
        if (this.ws) {
            this.ws.close();
        }
    }
}
export class PodcastGenerationWebSocket {
    private ws: WebSocket;
    private messageHandler: ((data: any) => void) | null = null;

    constructor(podcastId: string) {
        this.ws = new WebSocket(`ws://${window.location.host}/api/ws/podcast-generation/${podcastId}`);
        
        this.ws.onmessage = (event) => {
            if (this.messageHandler) {
                try {
                    const data = JSON.parse(event.data);
                    this.messageHandler(data);
                } catch (error) {
                    console.error('Failed to parse WebSocket message:', error);
                }
            }
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };
    }

    onMessage(handler: (data: any) => void) {
        this.messageHandler = handler;
    }

    close() {
        this.ws.close();
    }
}

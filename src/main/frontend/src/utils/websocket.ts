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
